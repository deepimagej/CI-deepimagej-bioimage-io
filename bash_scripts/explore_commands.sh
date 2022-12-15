#! /usr/bin/env sh

date
echo --
pwd
echo --
tree -L 2 -h --du --dirsfirst
echo --
ls $GITHUB_WORKSPACE | tee output.txt
echo --
echo ~
echo --
echo $HOME
