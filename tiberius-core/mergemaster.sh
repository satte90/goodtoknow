#!/bin/bash
echo "Merging with master and auto accepting their change"
git merge origin/master -X theirs
echo "Merge done. Now set new version manually!"
echo "Run:"
echo "mvn versions:set -DnewVersion=3.X.Y-ALPHA-Z"
echo "mvn versions:commit"
echo "git commit -m \"set new version 3.X.Y-ALPHA-Z\" ."
