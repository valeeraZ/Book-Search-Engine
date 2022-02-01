import Layout from "../components/layout";
import {Typography} from "@mui/material";
import * as React from "react";
import getAllBooks from "../lib/getAllBooks";
import getBooksByKeyword from "../lib/getBooksByKeyword";
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemText from '@mui/material/ListItemText';
import ListItemButton from '@mui/material/ListItemButton';
import Divider from '@mui/material/Divider';
import getBooksByRegex from "../lib/getBooksByRegex";
import getBooksByAuthor from "../lib/getBooksByAuthor";
import getBooksByTitle from "../lib/getBooksByTitle";
import Link from 'next/link'

const style = {
    width: '100%',
    bgcolor: 'background.paper',
};

function listBooks(books) {
    console.log(books)
    return (
        <List sx={style} component="nav" aria-label="books searching result">
            {books.map((book) => (
                <Link href={`/book?id=${book.id}`} passHref key={book.id}>
                    <ListItemButton >
                    <img
                        src={`${(book.image).replace('medium', 'small')}`}
                        srcSet={`${(book.image).replace('medium', 'small')}`}
                        alt={book.title}
                        loading="lazy"
                        height="100%"
                        style={{padding: '2em'}}
                    />
                    <ListItemText primary={book.title}
                                  secondary={
                                      <React.Fragment>
                                          {book.authors.length > 0 &&
                                          <Typography variant="body2">
                                              Author: &nbsp;
                                              {book.authors.map((author) => (
                                                  <span key={author.name}>
                                                                {(author.name).replace(', ', '-')}&nbsp;
                                                          </span>
                                              ))}
                                          </Typography>
                                          }

                                          {book.translators.length > 0 &&
                                          <Typography variant="body2">
                                              Translators: &nbsp;
                                              {book.translators.map((translator) => (
                                                  <span key={translator.name}>
                                                                {(translator.name).replace(', ', '-')}
                                                      &nbsp;
                                                            </span>
                                              ))}
                                          </Typography>
                                          }
                                          {book.bookshelves.length > 0 &&
                                          <Typography variant="body2">
                                              Bookshelves: &nbsp;
                                              {book.bookshelves.map((bookshelf) => (
                                                  <i key={bookshelf}>{bookshelf} &nbsp; </i>
                                              ))}
                                          </Typography>
                                          }
                                          {book.subjects.length > 0 &&
                                          <Typography variant="body2">
                                              Subject: &nbsp;
                                              {book.subjects.map((subject) => (
                                                  <i key={subject}>{subject} &nbsp; </i>
                                              ))}
                                          </Typography>
                                          }
                                      </React.Fragment>
                                  }
                    />
                </ListItemButton>
                </Link>
            ))}
        </List>
    )
}

export default function Search({data}) {
    const keyword = data.keyword
    const books = data.data
    return (
        <Layout>
            {books.booksByTitle.length > 0 &&
            <div>
                <Typography variant="h6" gutterBottom component="div" style={{marginTop: "2em"}}>
                    Results of books&apos; titles containing <i>{keyword}</i>
                </Typography>
                {listBooks(books.booksByTitle)}
            </div>
            }
            {books.booksByAuthor.length > 0 &&
                <div>
                    <Typography variant="h6" gutterBottom component="div" style={{marginTop: "2em"}}>
                        Results of books&apos; authors&apos; names containing <i>{keyword}</i>
                    </Typography>
                    {listBooks(books.booksByAuthor)}
                </div>
            }
            {books.booksByKeywordOrRegex.length > 0 &&
                <div>
                    <Typography variant="h6" gutterBottom component="div" style={{marginTop: "2em"}}>
                        Results of books&apos; content containing <i>{keyword}</i>
                    </Typography>
                    {listBooks(books.booksByKeywordOrRegex)}
                </div>
            }

        </Layout>
    )

}

Array.prototype.unique = function () {
    var a = this.concat();
    for (var i = 0; i < a.length; ++i) {
        for (var j = i + 1; j < a.length; ++j) {
            if (a[i] === a[j])
                a.splice(j--, 1);
        }
    }

    return a;
};

export async function getServerSideProps({query}) {
    const keywordOrRegex = query.keyword || "";

    const booksByKeyword = await getBooksByKeyword(keywordOrRegex, true)
    const booksByRegex = await getBooksByRegex(keywordOrRegex, true)
    const booksByAuthor = await getBooksByAuthor(keywordOrRegex)
    const booksByTitle = await getBooksByTitle(keywordOrRegex)
    const data = {
        keyword: keywordOrRegex,
        data: {
            booksByKeywordOrRegex: booksByKeyword.concat(booksByRegex).unique(),
            booksByTitle: booksByTitle.unique(),
            booksByAuthor: booksByAuthor.unique()
        }
    }
    return {
        props: {
            data
        }
    }
}
