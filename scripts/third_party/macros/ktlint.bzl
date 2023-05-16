"""
Macro for extracting the ktlint executable Jar file from the shell script provided as the primary
release mechanism on ktlint's GitHub project page.
"""

load("@rules_java//java:defs.bzl", "java_binary", "java_import")

def _extract_ktlint_jar_impl(ctx):
    input_file = ctx.attr.input_file.files.to_list()[0]
    output_file = ctx.outputs.output_file

    ctx.actions.run(
        outputs = [output_file],
        inputs = [input_file],
        tools = [ctx.executable._generate_tool],
        executable = ctx.executable._generate_tool.path,
        arguments = [
            ".",  # Working directory of the Bazel repository (this is ignored).
            "generate",  # Script mode.
            input_file.path,  # Path to the source shell script.
            output_file.path,  # Path to the output Jar file.
        ],
        mnemonic = "ExtractKtlintJar",
        progress_message = "Extracting ktlint Jar from shell file",
    )
    return DefaultInfo(
        files = depset([output_file]),
        runfiles = ctx.runfiles(files = [input_file, output_file]),
    )

_extract_ktlint_jar = rule(
    attrs = {
        "input_file": attr.label(
            allow_single_file = True,
            mandatory = True,
        ),
        "output_file": attr.output(mandatory = True),
        "_generate_tool": attr.label(
            executable = True,
            cfg = "host",
            default = "//scripts:ktlint",
        ),
    },
    implementation = _extract_ktlint_jar_impl,
)

def extract_ktlint_jar(name, input_ktlint_shell_file, main_class):
    _extract_ktlint_jar(
        name = "%s_extracted_jar" % name,
        input_file = input_ktlint_shell_file,
        output_file = "%s_extraction.jar" % name,
    )
    java_import(
        name = "%s_imported" % name,
        jars = [":%s_extraction.jar" % name],
    )
    java_binary(
        name = name,
        runtime_deps = [":%s_imported" % name],
        main_class = main_class,
    )
