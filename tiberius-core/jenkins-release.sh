#!/bin/zsh

echo "Finding latest tagged release..."

git fetch --tags

latestTag=$(git describe --tags `git rev-list --tags --max-count=1`)

git checkout "$latestTag"

echo "Checked out tag: ${latestTag}"

git push bitbucket HEAD:master

git checkout master

echo "Pushed ${latestTag} to bitbucket/master"
