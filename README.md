# javadocky

This project is a clone of [javadoc.io](http://javadoc.io/).
This is also a sandbox project to play with spring-boot v2.5.2, spring-webflux v5.3.8 and selenide v5.2.8.

![Build Status](https://github.com/KengoTODA/javadocky/workflows/Build/badge.svg)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=javadocky&metric=coverage)](https://sonarcloud.io/dashboard?id=javadocky)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/14582f2758734bd3a9c2076e210e4174)](https://www.codacy.com/gh/KengoTODA/javadocky/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=KengoTODA/javadocky&amp;utm_campaign=Badge_Grade)

## How to build

```sh
$ docker-compose up --build
```

You can visit [http://localhost:8080/](http://localhost:8080/) to enjoy service.


### How to configure

You can [set property](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html), to configure Javadocky.

|name                      |default value                     |note    |
|--------------------------|----------------------------------|--------|
|javadocky.maven.repository|https://repo.maven.apache.org/maven2/  |URL of the Maven repository to download javadoc.jar|

## License

Copyright 2017-2022 Kengo TODA

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
