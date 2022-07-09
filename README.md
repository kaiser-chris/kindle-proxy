# Kindle Proxy

This application proxies different web novel pages and renders them in a Kindle friendly web page.

## Supported Pages

 - Royal Road

## Tested Devices

 - Kindle Paperwhite 2

Here is a guide to identify which kindle you own: https://www.androidauthority.com/which-kindle-model-do-i-have-1073996/

# Running it yourself

To run the Kindle Proxy it is recommended to use the provided docker images.
Images are currently published to the GitHub package registry.

## Environment Variables

These variables can be set to configure your setup.

| Variable           | Description                                                                                                                                                                                                                                           |
|--------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ROYAL_ROAD_PROFILE | Set this to your Royal Road public profile id (can be found in the url) to show only your favorite items in the book overview. When it is left blank only the 'Best Rated' books are listed since Royal Road has too much content to list everything. |

## Examples

Below are a few simple examples to run the service using docker.

### docker compose

```
version: "3.9"
services:
  kindle-proxy:
    image: ghcr.io/kaiser-chris/kindle-proxy:latest
    ports:
      - 80:80
```

### docker run

```
docker run -d -p 80:80 ghcr.io/kaiser-chris/kindle-proxy:latest
```

# Image Attribution

 - [Home Icon](/src/main/resources/static/img/home-solid.svg) by [Font Awesome](https://fontawesome.com/license)
 - [Arrow Left](/src/main/resources/static/img/arrow-left-solid.svg) by [Font Awesome](https://fontawesome.com/license)
 - [Arrow Right](/src/main/resources/static/img/arrow-right-solid.svg) by [Font Awesome](https://fontawesome.com/license)
 - [Sync Icon](/src/main/resources/static/img/sync-solid.svg) by [Font Awesome](https://fontawesome.com/license)
 - [List Icon](/src/main/resources/static/img/list-solid.svg) by [Font Awesome](https://fontawesome.com/license)
 - [Search Icon](/src/main/resources/static/img/search-solid.svg) by [Font Awesome](https://fontawesome.com/license)