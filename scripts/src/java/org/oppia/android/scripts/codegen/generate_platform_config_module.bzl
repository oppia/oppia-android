def _generate_platform_config_module_impl(ctx):
    output_file = ctx.outputs.output_file
    qualified_class_name = ctx.attr.qualified_class_name
    platform_parameter_definitions = ctx.attr.platform_parameter_definitions.files.to_list()[0]
    feature_flag_definitions = ctx.attr.feature_flag_definitions.files.to_list()[0]

    arguments = ctx.actions.args()
    arguments.add(qualified_class_name)
    arguments.add(output_file)
    arguments.add(platform_parameter_definitions)
    arguments.add(feature_flag_definitions)
    ctx.actions.run(
        inputs = [platform_parameter_definitions, feature_flag_definitions],
        outputs = [output_file],
        tools = [ctx.executable._generate_platform_config_module_tool],
        executable = ctx.executable._generate_platform_config_module_tool.path,
        arguments = [arguments],
    )

    return DefaultInfo(
        files = depset([output_file]),
        runfiles = ctx.runfiles(files = [output_file]),
    )

_generate_platform_config_module = rule(
    attrs = {
        "qualified_class_name": attr.string(mandatory = True),
        "platform_parameter_definitions": attr.label(default = "//config/src/java/org/oppia/android/config:platform_parameter_definitions"),
        "feature_flag_definitions": attr.label(default = "//config/src/java/org/oppia/android/config:feature_flag_definitions"),
        "output_file": attr.output(
            mandatory = True,
        ),
        "_generate_platform_config_module_tool": attr.label(
            executable = True,
            cfg = "host",
            default = "//scripts:generate_platform_config_module",
        ),
    },
    implementation = _generate_platform_config_module_impl,
)

def generate_platform_config_module(name, qualified_class_name, output_file, **kwargs):
    _generate_platform_config_module(
        name = name,
        qualified_class_name = qualified_class_name,
        output_file = output_file,
        **kwargs
    )
