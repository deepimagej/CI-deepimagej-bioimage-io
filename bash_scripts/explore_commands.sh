#! /usr/bin/env sh

date
echo --
pwd
echo --
ls -la
echo --
ls $GITHUB_WORKSPACE | tee output.txt
echo --
echo ~
echo --
echo $HOME
