# Output of first run with some commands
- Checking out changes the `$GITHUB_WORKSPACE` env variable
````
Run date
  date
  uname -a
  java -version
  pwd
  ls -la
  echo --
  ls -la .. 
  ls $GITHUB_WORKSPACE
  shell: /usr/bin/bash -e {0}
Fri Dec  9 13:01:31 UTC 2022
Linux fv-az562-837 5.15.0-1024-azure #30-Ubuntu SMP Wed Nov 16 23:37:59 UTC 2022 x86_64 x86_64 x86_64 GNU/Linux
openjdk version "11.0.17" 2022-10-18
OpenJDK Runtime Environment Temurin-11.0.17+8 (build 11.0.17+8)
OpenJDK 64-Bit Server VM Temurin-11.0.17+8 (build 11.0.17+8, mixed mode)
/home/runner/work/CI-deepimagej-bioimage-io/CI-deepimagej-bioimage-io
total 28
drwxr-xr-x 4 runner docker 4096 Dec  9 13:31 .
drwxr-xr-x 3 runner docker 4096 Dec  9 13:31 ..
drwxr-xr-x 8 runner docker 4096 Dec  9 13:31 .git
drwxr-xr-x 3 runner docker 4096 Dec  9 13:31 .github
-rw-r--r-- 1 runner docker   32 Dec  9 13:31 .gitignore
-rw-r--r-- 1 runner docker  116 Dec  9 13:31 README.md
-rw-r--r-- 1 runner docker  542 Dec  9 13:31 lessons_learnt.md
--
total 12
drwxr-xr-x 3 runner docker 4096 Dec  9 13:31 .
drwxr-xr-x 6 runner root   4096 Dec  9 13:31 ..
drwxr-xr-x 4 runner docker 4096 Dec  9 13:31 CI-deepimagej-bioimage-io
README.md
lessons_learnt.md
````