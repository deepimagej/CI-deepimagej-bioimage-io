# To-Do's
## Use constants from config
- [x] refactor `reproduce.communicate.clj`
- [ ] read serialized config in `test_1_with_deepimagej.clj`

## Refactor utils
- [x] put more generally used functions in `utils.clj`
    + [x] my-time
    + [x] print-and-log
- [x] test of those functions in `utils-test.clj`

## Refactors
- [ ] improve utils/new-root-path
- [ ] think of deleting unused code (?)
  + pros: 
    + smaller codebase
    + easier to understand (newcomer / reentering the code)
    + what there is is what is executing
    + if needed, it is in the repository
  + cons: 
    + need to delete also tests
    + if needed, need to find it in the repository



## Functionality
### Reproduce pipeline from local directory of models (no need for init or download)
- Difference local and global rdfs
- problem in downloads/get-url-filename and communicate/get-name-from-url
- [ ] dispatch if url is string URL or File (?)

### Other functionality
- [x] Basic report automatically on gh-pages readme.
- [ ] Download with cache
  + cache directory
  + max cache duration
- [ ] Return of download
  + iso time string / java.time.TimeDifference
  + megabytes written string / long 
  + cached time (nil if it was actually downloaded)