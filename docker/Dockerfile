# Use the official Ubuntu base image
FROM ubuntu:latest

# Set environment variables to avoid interactive prompts during package installations
ENV DEBIAN_FRONTEND=noninteractive

# Update the package list, install required packages, and clean up to save space
RUN apt-get update && apt-get install -y --no-install-recommends \
    wget \
    curl \
    gnupg \
    software-properties-common \
    build-essential \
    git \
    unzip \
    apt-transport-https && \
    rm -rf /var/lib/apt/lists/*

# Install OpenJDK 11 and OpenJDK 17
RUN apt-get update && apt-get install -y --no-install-recommends \
    openjdk-11-jdk \
    openjdk-17-jdk && \
    rm -rf /var/lib/apt/lists/*

# Set environment variables for JDK 11 and JDK 17
ENV JAVA_HOME_11=/usr/lib/jvm/java-11-openjdk-amd64
ENV JAVA_HOME_17=/usr/lib/jvm/java-17-openjdk-amd64

# Set default JAVA_HOME to JDK 11
ENV JAVA_HOME=$JAVA_HOME_11
ENV PATH=$JAVA_HOME/bin:$PATH

# Provide commands to switch between JDK 11 and JDK 17
RUN echo "export JAVA_HOME=\$JAVA_HOME_11 && export PATH=\$JAVA_HOME/bin:\$PATH" > /etc/profile.d/jdk11.sh && \
    echo "export JAVA_HOME=\$JAVA_HOME_17 && export PATH=\$JAVA_HOME/bin:\$PATH" > /etc/profile.d/jdk17.sh && \
    chmod +x /etc/profile.d/jdk11.sh /etc/profile.d/jdk17.sh

# Install Bazel dependencies and GPG key
RUN curl -fsSL https://bazel.build/bazel-release.pub.gpg | gpg --dearmor > /usr/share/keyrings/bazel-archive-keyring.gpg && \
    echo "deb [arch=amd64 signed-by=/usr/share/keyrings/bazel-archive-keyring.gpg] https://storage.googleapis.com/bazel-apt stable jdk1.8" > /etc/apt/sources.list.d/bazel.list && \
    apt-get update && apt-get install -y --no-install-recommends bazel && \
    apt-get install -y --no-install-recommends bazel-6.5.0 && \
    rm -rf /var/lib/apt/lists/*

# Set up Android SDK directory and download command-line tools
ENV ANDROID_HOME=/opt/android-sdk
RUN mkdir -p $ANDROID_HOME/cmdline-tools && \
    wget https://dl.google.com/android/repository/commandlinetools-linux-7302050_latest.zip -O /tmp/commandlinetools.zip && \
    unzip /tmp/commandlinetools.zip -d $ANDROID_HOME/cmdline-tools && \
    mv $ANDROID_HOME/cmdline-tools/cmdline-tools $ANDROID_HOME/cmdline-tools/tools && \
    rm /tmp/commandlinetools.zip

# Update environment variables for Android SDK
ENV PATH=$ANDROID_HOME/cmdline-tools/tools/bin:$ANDROID_HOME/platform-tools:$PATH

# Accept Android SDK licenses and install required components
RUN yes | sdkmanager --licenses && \
    sdkmanager --install "platform-tools" "platforms;android-34" "build-tools;32.0.0"

# Configure Bazel for Linux
RUN echo "build --enable_platform_specific_config" >> $HOME/.bazelrc && \
    echo "build:linux --sandbox_tmpfs_path=/tmp" >> $HOME/.bazelrc


# Default command to run when the container starts
CMD ["bash"]
