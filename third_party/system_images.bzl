load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("//third_party:system_images_list.bzl", "SYSTEM_IMAGES_LIST")

def provide_system_image_archives():
    images_to_download = {}
    for path, os_split_system_image in SYSTEM_IMAGES_LIST.items():
        for os_name, system_image_definition in os_split_system_image.items():
            sha256 = system_image_definition["sha256"]
            if sha256 not in images_to_download:
                images_to_download[sha256] = system_image_definition["url"]
    for sha256, url in images_to_download.items():
        http_archive(
            name = "system_image_%s" % sha256,
            build_file_content = """
filegroup(
    name = "package_files",
    srcs = glob(["*/**"]),
    visibility = ["//visibility:public"],
)
            """,
            sha256 = sha256,
            url = url,
        )

def system_image_archive(path, os_split_system_image):
    # Selection logic is based on https://stackoverflow.com/a/48877306/3689782. The reference to
    # third_party is a hack to to define a new_local_repository who's entire purpose is provide a
    # nicer reference to system images.
    native.new_local_repository(
        name = "%s" % image_path_to_target_name(path),
        path = "third_party",
        build_file_content = """
alias(
    name = "package_files",
    actual = select({
        "@bazel_tools//src/conditions:linux_x86_64": "@imported_system_image_linux//:package_files",
        "@bazel_tools//src/conditions:darwin": "@imported_system_image_mac_osx//:package_files",
        "@bazel_tools//src/conditions:windows": "@imported_system_image_windows//:package_files",
    }),
    visibility = ["//visibility:public"],
)
        """,
        repo_mapping = {
            "@imported_system_image_%s" % os_name: (
                "@system_image_%s" % os_split_system_image[os_name]["sha256"]
            )
            for os_name in os_split_system_image.keys()
        },
    )

def compute_system_image_path(api_level, tag, abi):
    return "system-images;android-%s;%s;%s" % (api_level, tag, abi)

def image_path_to_target_name(image_path):
    return image_path.replace(";", "_")
