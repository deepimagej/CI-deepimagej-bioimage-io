# To-Do's
## Use constants from config
- [x] refactor `reproduce.communicate.clj`
- [ ] read serialized config in `test_1_with_deepimagej.clj`

## Refactor utils
- [ ] put more generally used functions in `utils.clj`
    + [ ] my-time
    + [x] print-and-log
- [ ] test of those functions in `utils-test.clj`

## Functionality

- [ ] reproduce pipeline from local directory of models (no need for init or download) 
- [ ] Basic report automatically on gh-pages readme.
- [ ] Download with cache
  + cache directory
  + max cache duration
- [ ] Return of download
  + iso time string / java.time.TimeDifference
  + megabytes written string / long 
  + cached time (nil if it was actually downloaded)