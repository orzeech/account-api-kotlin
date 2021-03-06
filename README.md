# Account API

This application is an example of Kotlin reactive API.

## What's implemented so far

* Basic API endpoints: `GET /account/{id}` and `GET /account/{id}/balance/{currency}`
* Some tests
* Very basic cache
* Calling NBP's API to fetch currency exchange rates
* H2 In-memory DB or a plain Map as data storage

## What's not implemented

* Save / update endpoints
* Tests for repositories
* Fancier cache strategy
* E2E tests
* Concurrency

## How to use

Start this Spring Boot application with `h2` profile to persist data with in-memory H2 SQL Database
or with `map` profile to persist data in a map. 