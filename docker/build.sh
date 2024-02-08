#! /bin/bash

docker build \
       --build-arg SOUFFLE_GIT=https://github.com/alexdura/souffle \
       --build-arg SOUFFLE_HASH=84826bb8b6ef739a762c992c06a42441d19b72da \
       --build-arg CLOG_GIT=https://github.com/lu-cs-sde/clog.git \
       --build-arg CLOG_HASH=b16078260dca9e6437675c2715fdbd4c349a85a4 \
       --build-arg CLOG_EVAL_GIT=https://github.com/alexdura/clog-eval.git \
       --build-arg CLOG_EVAL_HASH=9458c0d2fc62815796e07600d1fa779ea05aa020 \
       --build-arg LLVM_GIT=https://github.com/alexdura/llvm-project.git \
       --build-arg LLVM_HASH=c9a1d5ea1edc7439f2213fd42a080828d0e7de22 \
       --build-arg JULIET_GIT=https://github.com/alexdura/juliet.git \
       --build-arg JULIET_HASH=dc4849eb6072f1fb1dec861aa2eb0e34ba379c1a \
       --build-arg MAGMA_GIT=https://github.com/alexdura/magma.git \
       --build-arg MAGMA_HASH=07b321d2ba6d94ed8e26d780d3e079a68e06a682 \
       -t clog23 .
