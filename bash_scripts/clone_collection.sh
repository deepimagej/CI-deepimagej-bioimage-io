#! /usr/bin/env sh

name="bioimageio-gh-pages"
if [ ! -d "../$name" ]; then
git clone -b gh-pages https://github.com/bioimage-io/collection-bioimage-io.git "../$name"
else
echo "Directory ../$name already exists"
fi

  
