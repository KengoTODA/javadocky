# javadocky

This project is a clone of [javadoc.io](http://javadoc.io/).
This is also a sandbox project to play with spring-boot v2.1, spring-webflux v5.0 and selenide v5.2.

![Build Status](https://github.com/KengoTODA/javadocky/workflows/Build/badge.svg)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=javadocky&metric=coverage)](https://sonarcloud.io/dashboard?id=javadocky)
[![Maintainability](https://api.codeclimate.com/v1/badges/f021dc49dcefcb5fd337/maintainability)](https://codeclimate.com/github/KengoTODA/javadocky/maintainability)

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

Copyright 2017-2021 Kengo TODA

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
