# tofq

[![Build Status](https://travis-ci.org/aornice/tofq.svg?branch=master)](https://travis-ci.org/aornice/tofq)
[![Coverage
Status](https://coveralls.io/repos/github/aornice/tofq/badge.svg?branch=master)](https://coveralls.io/github/aornice/tofq?branch=master)
[![License](https://img.shields.io/dub/l/vibe-d.svg)](https://opensource.org/licenses/MIT)

Tofq is a distributed persisted queue inspired by [Apache Kafka](http://kafka.apache.org/).

## Development

Development of tofq has three stages:

1. Local persisted queue
2. network support
3. distrubution

## Design

### Data Structure

All data of Tofq is stored in files. Table of contents is like below:

```
-- root
    -- topic1
        -- data0-0.tofq
        -- data0-1.tofq
        -- ...
        -- data1-0.tofq
    -- topic2
```

A file ending with "tofq" has three parts: headers, indexes and messages. The count of messages in each file is fixed. You can quickly find messages by date or sequence.

### Persistence

Tofq makes full usage of the disk space you have, so you are not limited by the main memory of your machine. 

Tofq stores all saved data in memory mapped files, which has little influence on heap overhead.

## License

[MIT License](./LICENSE)