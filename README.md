wkhtmltopdf-binary
==================

wkhtmltopdf-binary provides the [wkhtmltopdf](http://wkhtmltopdf.org) binary
packaged in a Jar as dependency for Java applications.

Note: The current version only supports Linux (amd64) architecture, but this
could easily be extended (pull requests are welcome!).

Usage
-----
The Jar file can be build with [Gradle].

    gradle assemble

The resulting Jar file can be found in ```build/libs/```.

[Gradle]: http://gradle.org

In your Java code, you can get the path of the wkhtmltopdf binary.

```java
URI exe = WkHtmlToPdfBinary.getInstance().getExe();
String[] params = new String[]{
    exe.getPath(),
    htmlFile.getCanonicalPath(),
    "--header-html",
    htmlHeaderFile.getCanonicalPath(),
    outputFile.getCanonicalPath()
};
Process process = new ProcessBuilder(params).start();
```

Copyright and license
---------------------

Copyright 2016 Tocco AG

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this work except in compliance with the License.
You may obtain a copy of the License in the LICENSE file, or at:

  [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
