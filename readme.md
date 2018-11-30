# Spring / Keycloak Multitenant Tutorial

This is a sandbox / sample project to illustrate the multi-tenant with Keycloak and Spring Boot
[Multi tenant with Keycloak](https://blog.ineat-conseil.fr/2018/11/securisez-vos-apis-spring-avec-keycloak-5-mise-en-place-dune-authentification-multi-domaines/)

## How to start

> Start your Keycloak server before running the app
> * How to install & start Keycloak : http://blog.ineat-conseil.fr/2017/11/securisez-vos-apis-spring-avec-keycloak-1-installation-de-keycloak/
> * How to create a realm for this project :
>   * Click on "Import" in Keycloak main menu
>   * Click on "Select file", and find realm-ORGA1-export.json.
>   * Turn on "Import clients", "Import realm roles" and "Import client roles".
>   * Click on "Import"
>   * Repeat previous steps for realm-ORGA2-export.json

```shell
mvn spring-boot:run
```
