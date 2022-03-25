const GithubApi = require('@octokit/rest')

const statusMap = {
  QUEUED: "pending",
  WORKING: "pending",
  SUCCESS: "success",
  FAILURE: "failure",
  CANCELLED: "failure",
  TIMEOUT: "error",
  INTERNAL_ERROR: "error"
}

// The main function
module.exports.updateBuildStatus = (event) => {
  build = eventToBuild(event.data.data)
  console.log(`gcloud container builds describe --format=json ${build.id}`)

  const {
    id,
    projectId,
    status,
    steps,
    sourceProvenance: {
      resolvedRepoSource: repoSource
    },
    logUrl,
    tags,
    createTime,
    finishTime,
  } = build

  const ghStatus = statusMap[status]

  if (!repoSource || !ghStatus) {
    console.log(`No repo source or invalid status: ${JSON.stringify(build)}`)
    return
  }

  let i, j = 0
  const delimiter = repoSource.repoName.startsWith('github_') ? '_' : '-';
  i = repoSource.repoName.indexOf(delimiter)
  j = repoSource.repoName.indexOf(delimiter, i + 1)
  const repoOwner = repoSource.repoName.substring(i + 1, j)
  const repoName = repoSource.repoName.substring(j + 1)
  if (!repoOwner || !repoName) {
    console.log(`Invalid repo info: ${repoSource.repoName}`)
    return
  }

  const prettyTags = tags && tags.filter(t => !t.match(/(event|trigger|eval|invocation)-[\w-]{36}/))
  const ghContext = prettyTags && prettyTags.length > 0
    ? `Google Cloud Build: ${projectId}/${prettyTags.join('/')}`
    : `Google Cloud Build: ${projectId}`

  const lastStep = steps.filter( s => s.timing && s.timing.startTime ).pop()
  const failureDescription = (ghStatus=='failure' || ghStatus=='error')
    ? ' · ' + (lastStep ? `${lastStep.id} `:'') + status.toLowerCase()
    : ''
  const ghDescription = (
    createTime && finishTime
    ? secondsToString((new Date(finishTime) - new Date(createTime)) / 1000) + failureDescription
    : `${titleize(status)}`
  ).substring(0,140)

  console.log(status, ghStatus)
  console.log(repoName, repoSource)
  console.log(ghContext, tags)
  console.log(ghDescription, createTime, finishTime)

  let github = new GithubApi()
  github.authenticate({
    type: 'token',
    token: process.env.GITHUB_ACCESS_TOKEN
  })

  let request = {
    owner: repoOwner,
    repo: repoName,
    sha: repoSource.commitSha,
    state: ghStatus,
    target_url: logUrl,
    description: ghDescription,
    context: ghContext
  }

  console.log(JSON.stringify(request, null, 2))

  return github.repos.createStatus(request)
}

// eventToBuild transforms pubsub event message to a build object.
const eventToBuild = (data) =>
  JSON.parse(new Buffer(data, 'base64').toString())

// secondsToString turns a number of seconds into a human-readable duration.
const secondsToString = (s) => {
  const years   = Math.floor(s / 31536000)
  const days    = Math.floor((s % 31536000) / 86400)
  const hours   = Math.floor(((s % 31536000) % 86400) / 3600)
  const minutes = Math.floor((((s % 31536000) % 86400) % 3600) / 60)
  const seconds = Math.floor((((s % 31536000) % 86400) % 3600) % 60)

  return `${years}y ${days}d ${hours}h ${minutes}m ${seconds}s`
    .replace(/^(0[ydhm] )*/g, '')
}

const titleize = (s) => {
  return s.charAt(0).toUpperCase() + s.slice(1).toLowerCase()
}
