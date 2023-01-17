#! /usr/bin/env sh

# initial setup for local testing
# this scripts are to run locally
# the workflows do this with action/checkout

# clone the collection locally

name="bioimageio-gh-pages"
if [ ! -d "../$name" ]; then
git clone -b gh-pages https://github.com/bioimage-io/collection-bioimage-io.git "../$name"
else
echo "Directory ../$name already exists"
fi

# clone the repository with the tiff images
name="numpy-tiff-deepimagej"
if [ ! -d "../$name" ]; then
git clone https://github.com/ivan-ea/numpy-tiff-deepimagej.git "../$name"
else
echo "Directory ../$name already exists"
fi
