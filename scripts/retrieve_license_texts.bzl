"""
Macro and rule corresponding to third-party dependencies license retrieval.
"""

def _retrieve_license_texts_impl(ctx):
    output_file = ctx.actions.declare_file(ctx.attr.output_file_name)
    maven_dependencies_pb = ctx.file._maven_dependencies_pb

    ctx.actions.run(
        outputs = [output_file],
        inputs = [maven_dependencies_pb],
        tools = [ctx.executable._retriever_tool],
        executable = ctx.executable._retriever_tool.path,
        arguments = [
            output_file.path,
            maven_dependencies_pb.path,
        ],
        mnemonic = "RetrieveLicenseTexts",
        progress_message = "Retrieving third-party license texts",
        execution_requirements = {
            "requires-network": "",
        },
    )
    return DefaultInfo(files = depset([output_file]))

_retrieve_license_texts = rule(
    attrs = {
        "output_file_name": attr.string(mandatory = True),
        "_maven_dependencies_pb": attr.label(
            allow_single_file = True,
            default = "//scripts:assets/maven_dependencies.pb",
        ),
        "_retriever_tool": attr.label(
            executable = True,
            cfg = "host",
            default = "//scripts:retrieve_license_texts",
        ),
    },
    implementation = _retrieve_license_texts_impl,
)

def retrieve_license_texts(name, output_file):
    """
    Retrieves third-party dependencies' license texts from Maven coordinates.

    This runs a Kotlin utility tool over the network to generate the XML resource file
    detailing third-party licenses.

    Args:
        name: str. A unique name for this target.
        output_file: str. The output file path relative to this package directory
            (e.g. 'res/values/third_party_dependencies.xml').
    """
    _retrieve_license_texts(
        name = name,
        output_file_name = output_file,
    )
