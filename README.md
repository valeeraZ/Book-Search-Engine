# Search engine for a library

In this project, we refer to library any database containing sufficiently many
text documents. One such example is given at The Gutenberg Project, where documents are stored in various formats,
including ASCII text format. Much as with The Gutenberg Project’s database, a library can store tens of thousands of
text documents.   

A search engine in such a library is a web/mobile application whose primary feature is to point its users to the right
document, according to a search by keyword. Another feature could be to point the users to a recommended document
following their search history.

We use [Spring Boot](http://projects.spring.io/spring-boot/) to create the back end and [Next.js](https://nextjs.org/) to create the front end for this web app.

![demo](https://user-images.githubusercontent.com/39196828/152812390-939a98fc-7163-4326-b404-469f728a4757.png)

## Requirements

For building and running the application you need:

- [JDK 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- [Maven 3](https://maven.apache.org)
- [Node.js](https://nodejs.org/en/) version 10.13 or later
- 1.5G of free disk space for downloading 1680 books

## Demo

https://user-images.githubusercontent.com/39196828/152809569-69d718dc-cd41-4e6f-b97c-3e2cdbad9750.mov


## Running the application locally

### Starting the back end

First thing to do is `cd book_search_engine ` to enter the folder of back end or using any IDE (Eclipse, Intellij IDEA, VS Code...) to open this folder.

Before running the back end, for the first time use, you need to create a `books` folder to store the text files of 1680 books.

There are several ways to run a Spring Boot application on your local machine. One way is to execute the `main` method in the `com.sorbonne.book_search_engine.BookSearchEngineApplication` class from your IDE.

Alternatively you can use the [Spring Boot Maven plugin](https://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins-maven-plugin.html) like so:

```shell
mvn spring-boot:run
```

 The program will start downloading 1680 books from [Project Gutenberg](https://gutenberg.org/). The time of downloading might take some moments, which depends on your network quality. 

### Starting the front end

After the back end starts running, you can start the front end. Use `cd book_search_enging-front-end` or any IDE (VS Code, WebStorm...) to open this folder.

Make sure the back end is running, then you can use

```bash
npm run dev
# or
yarn dev
```

to run the front end. Open [http://localhost:3000](http://localhost:3000) with your browser to see the result.

## Documentation of API
See [Our Postman API Documenter](https://documenter.getpostman.com/view/10263827/UVRHiP1r).

## Copyright

Released under MIT Licence. 
