# **Tiberius User**

### Documentation on confluence:

[Tiberius Services](https://diva.teliacompany.net/confluence/x/L7eqBw)  
[Tiberius Core](https://diva.teliacompany.net/confluence/x/OxbbCQ)

## Working with git

This repository has two remotes that should be in sync:
* Github: https://github.com/telia-company/tiberius-user
* Bitbucket: https://diva.teliacompany.net/bitbucket/projects/DCVT/repos/tiberius-user/browse

The master repo is in __GitHub__ and the pipeline in github is responsible for promoting and setting new development versions.

### Remove / rename the current remote
If you have cloned this from either github or bitbucket, rename that remote to github or bitbucket, or remove it and re-add it using command below.

Run ```git remote -vv``` to show what remotes currently exist

Run ```git remote rm origin```

### Add remote(s) that is missing:
* ```git remote add github git@github.com:telia-company/tiberius-user.git```
* ```git remote add bitbucket ssh://git@diva.teliacompany.net:7999/dcvt/tiberius-user.git```

### Set default upstream remote
```git branch --set-upstream-to github/master```

### Verify remotes

Run ```git remote -vv``` again and you should have:
```
bitbucket  ssh://git@diva.teliacompany.net:7999/dcvt/tiberius-user.git (fetch)
bitbucket  ssh://git@diva.teliacompany.net:7999/dcvt/tiberius-user.git (push)
github	   git@github.com:telia-company/tiberius-user.git (fetch)
github	   git@github.com:telia-company/tiberius-user.git (push)
```

## Release to "Classic" (Bitbucket/jenkins pipeline)

If you have the remote repos set up like above you can simply run the ```jenkins-release.sh``` script.
It will fetch all tags and find the latest one, the check-out the commit for that tag
and push it to bitbucket/master. Finally, it will check-out master again.
