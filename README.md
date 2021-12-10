# Pockets ![GitHub Workflow Status](https://img.shields.io/github/workflow/status/ebullient/pockets-cli/Java%20CI%20with%20Maven) [![](https://jitpack.io/v/ebullient/pockets-cli.svg)](https://jitpack.io/#ebullient/pockets-cli) 

A D&amp;D Inventory tracking CLI

<table><tr><td>Jump: </td>
<td><a href="#install-it">Install It</a></td>
<td><a href="#use-it">Use it</a></td>
<td><a href="#build-it-optional">Build it (Optional)</a></td></tr></table>

This project is built using [Quarkus](https://quarkus.io), the Supersonic Subatomic Java Framework, and is packaged and distributed as non-invasively as possible with help from [JBang!](https://jbang.io). It uses Picocli, JPA, Hibernate, and H2.

Goals: 
- [x] Track the contents of multiple "pockets"
- [x] Add/remove items from a pocket
- [ ] Keep a record of additions, removals and exchanges (a ledger)
- [ ] Track cumulative value of items in a pocket (and then across pockets)
- [ ] Allow addition of standard items (with quick modifications as necessary)

## Install it

1. Install JBang: https://www.jbang.dev/documentation/guide/latest/installation.html
2. Install the snapshot jar: 
```
jbang app install --name pockets --force --fresh https://jitpack.io/dev/ebullient/pockets-cli/1.0.0-SNAPSHOT/pockets-cli-1.0.0-SNAPSHOT-runner.jar
```
3. Run the command: 
```
pockets --help
```

## Use it

```
Usage: pockets [-hvV] [COMMAND]
  -h, --help      Show this help message and exit.
  -v, --verbose   verbose output
  -V, --version   Print version information and exit.
Commands:
  create  Create a new pocket
  list    What do we have in our pockets?
  add     Add an item to a pocket
```


Commands: 

- list: What _have_ I got in my pockets?
- add: add something
- remove: remove something
- exchange: exchange something for something else of equal value



## Build it (optional)

Prerequisites: Java 11 and Maven

1. Clone this repository
2. Build this project: `quarkus build` or `./mvnw install`
3. `java -jar target/pockets-cli-1.0.0-SNAPSHOT-runner.jar --help`

If you want to use the snazzy alias with your freshly built local snapshot, JBang can help with that, too:
```
jbang app install --name pockets --force ~/.m2/repository/dev/ebullient/pockets-cli/1.0.0-SNAPSHOT/pockets-cli-1.0.0-SNAPSHOT-runner.jar
```

