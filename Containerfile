# Munitorum: native image build
#
# Stage 1 builds a fully static, musl-linked native binary with GraalVM, using the
# Maven Wrapper so the Maven version matches every other build. Stage 2 copies that
# single binary into a distroless static image (no shell, no package manager, no libc).
#
#   podman build -t munitorum -f Containerfile .
#
# Base images are pinned by tag and digest. Dependabot updates both. The images are
# written directly on the FROM lines because Dependabot can't see through ARG values.

# --- Native build ---
FROM ghcr.io/graalvm/native-image-community:25-muslib@sha256:bd08fee5f6effb210e829f434e5b5341d9b5b321ddc47363a241853723c810eb AS build

# mvnw unpacks the Maven distribution with unzip. Without it, mvnw fetches the .tar.gz
# instead, which doesn't match the checksum pinned in .mvn/wrapper.
# The image also sets LANG=en_US.UTF-8 without shipping that locale, so mvnw's shell
# calls print setlocale warnings. The langpack is pinned to the installed glibc version
# because a newer one would pull in a glibc that conflicts with glibc-static (which
# native-image needs for --static).
RUN microdnf install -y unzip "glibc-langpack-en-$(rpm -q --qf '%{VERSION}-%{RELEASE}' glibc)" \
 && microdnf clean all

WORKDIR /build

COPY mvnw pom.xml ./
COPY .mvn ./.mvn
COPY src ./src

# The cache mount keeps the Maven distribution and the local repository between builds.
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -ntp -Pnative -DskipTests package

# --- Runtime ---
FROM gcr.io/distroless/static-debian13:nonroot@sha256:25ac8f0c3b2334a9eb05452266bd4697864a8c8ef0ef71e5e6480d7f92868329

COPY --from=build /build/target/munitorum /munitorum

USER nonroot:nonroot
ENTRYPOINT ["/munitorum"]
