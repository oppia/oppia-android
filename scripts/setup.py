import shutil
import platform
import subprocess
import os

OS_NAME = platform.system()

# INSTRUCTIONS:
# This script will move the pre-commit script from scripts folder to .git/hooks folder
# to activate the checks prior to any commits.
#
# Run the script from the oppia-android root folder:
#
#   python scripts/setup.py
#

def install_hook():
    """Installs the pre_commit_hook script and makes it executable.
    It ensures that oppia-android/ is the root folder.
    """
    oppia_android_dir = os.getcwd()
    hooks_dir = os.path.join(oppia_android_dir, '.git', 'hooks')
    pre_commit_file = os.path.join(hooks_dir, 'pre-commit')
    chmod_cmd = ['chmod', '+x', pre_commit_file]
    file_exists = os.path.exists(pre_commit_file)
    if file_exists:
        print('Pre-commit hook already exists')
    else:
        shutil.copy('scripts/pre-commit', '.git/hooks/')
        print('Copied file to .git/hooks directory')

    print('Making pre-commit hook file executable ...')
    if not is_windows_os():
            _, err_chmod_cmd = start_subprocess_for_result(chmod_cmd)

            if not err_chmod_cmd:
                print('Pre-commit hook file is now executable!')
            else:
                raise ValueError(err_chmod_cmd)

def is_windows_os():
    """Check if the running system is Windows."""
    return OS_NAME == 'Windows'

def start_subprocess_for_result(cmd):
    """Starts subprocess and returns (stdout, stderr)."""
    task = subprocess.Popen(
        cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    out, err = task.communicate()
    return out, err

def main(args=None):
    """All the initial setup requirements functions will call from here."""
    install_hook()

if __name__ == '__main__':
    main()
