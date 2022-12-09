# Output of first run with some commands
- Checking out changes the `$GITHUB_WORKSPACE` env variable
````
Run date
  date
  uname -a
  pwd
  ls -l
  which tree
  which java
  ls $GITHUB_WORKSPACE
  shell: /usr/bin/bash -e {0}
Fri Dec  9 13:01:31 UTC 2022
Linux fv-az562-837 5.15.0-1024-azure #30-Ubuntu SMP Wed Nov 16 23:37:59 UTC 2022 x86_64 x86_64 x86_64 GNU/Linux
/home/runner/work/CI-deepimagej-bioimage-io/CI-deepimagej-bioimage-io
total 4
-rw-r--r-- 1 runner docker 116 Dec  9 13:01 README.md
/usr/bin/tree
/usr/bin/java
README.md
````
