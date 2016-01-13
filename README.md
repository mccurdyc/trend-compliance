# trend-compliance
Tool for working with parsing trend anti-virus compliance list. Finds IP
addresses that are not registered with the campus-required anti-virus.
After grabbing their IP, searches for their IP in a detailed log file
and gets the user's MAC address.

## Installation

### Clone
The easiest way is to clone this repository with the following command.

```
git clone https://github.com/mccurdyc/trend-compliance.git
```

Don't have Git? Just download the `.zip` file. You can do this by
visiting the follow URL.

```
https://codeload.github.com/mccurdyc/trend-compliance/zip/master
```

## Usage
First, change the filename of the wired, wireless, and detailed files to
something without spaces in the filename.

Then, you can just drag the files into the Terminal when prompted for
the files.

*Note: The files still need to be in a certain order!*

*Note: If you are on Windows, the following command will not work unless
you have `bash` installed.*

```
./tc.sh
```

#### Use this command on Windows

```
java -jar target/uberjar/trend-compliance-0.1.0-SNAPSHOT-standalone.jar
```

Then, you will need to follow the on-screen instructions.

## License

The MIT License (MIT)

Copyright (c) [year] [fullname]

Permission is hereby granted, free of charge, to any person obtaining a
copy
of this software and associated documentation files (the "Software"), to
deal
in the Software without restriction, including without limitation the
rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or
sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included
in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
IN THE
SOFTWARE.

