# coachmark

Setup
[![](https://jitpack.io/v/suryojiwandono/coachmark.svg)](https://jitpack.io/#suryojiwandono/coachmark)
====
1. Add it in your root build.gradle at the end of repositories:
~~~
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
~~~
Or in your settings.gradle
~~~
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
~~~

2. Add the dependency:
~~~
dependencies {
  ...
  implementation 'com.github.suryojiwandono:coachmark:0.0.3'
}
~~~

If you're using Maven, you can add the APKlib as a dependency:

```xml
<dependency>
    <groupId>com.github.suryojiwandono</groupId>
    <artifactId>coachmark</artifactId>
    <version>0.0.3</version>
</dependency>
```


## License
```
Copyright 2022 Suryo Jiwandono Guntoro
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
---
