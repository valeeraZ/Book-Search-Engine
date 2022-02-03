import getBookById from "../lib/getBookById";
import {Stack, Typography} from "@mui/material";
import * as React from "react";
import BookIcon from '@mui/icons-material/Book';
import Link from "@mui/material/Link";
import Layout from "../components/layout";
import {useRouter} from "next/router";
import Button from '@mui/material/Button';
import ArrowBackIosIcon from '@mui/icons-material/ArrowBackIos';
import getBooksBySuggestions from "../lib/getBooksBySuggestions";
import ListItemButton from "@mui/material/ListItemButton";
import ListItemText from "@mui/material/ListItemText";

export default function Book({data}) {
    const router = useRouter()
    const book = data.book
    const suggestion = data.suggestion
    return (
        <Layout>
            <Button onClick={() => router.back()} style={{marginTop:"2em"}}>
                <ArrowBackIosIcon/> Back
            </Button>
            <Stack spacing={2}>
                <Typography variant="h5" gutterBottom component="div" style={{marginTop: "2em"}}>
                    Information about book <i>{book.title}</i>
                </Typography>
                <div>
                    <img
                        src={`${(book.image).replace('small', 'medium')}`}
                        srcSet={`${(book.image).replace('small', 'medium')}`}
                        alt={book.title}
                        loading="lazy"
                        height="100%"
                    />
                </div>

                {book.authors.length > 0 &&
                <Typography variant="subtitle1">
                    Author: &nbsp;
                    {book.authors.map((author) => (
                        <span key={author.name}>
                                {(author.name).replace(', ', '-')}&nbsp;
                            </span>
                    ))}
                </Typography>
                }

                {book.translators.length > 0 &&
                <Typography variant="subtitle1">
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
                <Typography variant="subtitle1">
                    Bookshelves: &nbsp;
                    {book.bookshelves.map((bookshelf) => (
                        <i key={bookshelf}>{bookshelf} &nbsp; </i>
                    ))}
                </Typography>
                }
                {book.subjects.length > 0 &&
                <Typography variant="subtitle1">
                    Subject: &nbsp;
                    {book.subjects.map((subject) => (
                        <i key={subject}>{subject} &nbsp; </i>
                    ))}
                </Typography>
                }

                {book.languages.length > 0 &&
                <Typography variant="subtitle1">
                    Language: &nbsp;
                    {book.languages.map((language) => (
                        <span key={language}>{language} &nbsp;</span>
                    ))}
                </Typography>
                }
                <div>
                    <BookIcon/> <Link href={`/text?id=${book.id}`} variant="subtitle1">Read this book</Link>
                </div>
                <Typography variant="h6" gutterBottom component="div" style={{marginTop: "2em"}}>
                    You might also like...
                </Typography>
                <Link href={`/book?id=${suggestion.id}`} passHref>
                    <ListItemButton>
                        <img
                            src={`${(suggestion.image).replace('medium', 'small')}`}
                            srcSet={`${(suggestion.image).replace('medium', 'small')}`}
                            alt={suggestion.title}
                            loading="lazy"
                            height="100%"
                            style={{padding: '2em'}}
                        />
                        <ListItemText primary={suggestion.title}
                                      secondary={
                                          <React.Fragment>
                                              {suggestion.authors.length > 0 &&
                                              <Typography variant="body2">
                                                  Author: &nbsp;
                                                  {suggestion.authors.map((author) => (
                                                      <span key={author.name}>
                                                                {(author.name).replace(', ', '-')}&nbsp;
                                                          </span>
                                                  ))}
                                              </Typography>
                                              }

                                              {suggestion.translators.length > 0 &&
                                              <Typography variant="body2">
                                                  Translators: &nbsp;
                                                  {suggestion.translators.map((translator) => (
                                                      <span key={translator.name}>
                                                                {(translator.name).replace(', ', '-')}
                                                          &nbsp;
                                                            </span>
                                                  ))}
                                              </Typography>
                                              }
                                              {suggestion.bookshelves.length > 0 &&
                                              <Typography variant="body2">
                                                  Bookshelves: &nbsp;
                                                  {suggestion.bookshelves.map((bookshelf) => (
                                                      <i key={bookshelf}>{bookshelf} &nbsp; </i>
                                                  ))}
                                              </Typography>
                                              }
                                              {suggestion.subjects.length > 0 &&
                                              <Typography variant="body2">
                                                  Subject: &nbsp;
                                                  {suggestion.subjects.map((subject) => (
                                                      <i key={subject}>{subject} &nbsp; </i>
                                                  ))}
                                              </Typography>
                                              }
                                          </React.Fragment>
                                      }
                        />
                    </ListItemButton>
                </Link>
            </Stack>
        </Layout>
    )

}

export async function getServerSideProps({query}) {
    const id = query.id || 1;
    const book = await getBookById(id)
    const suggestion = await getBooksBySuggestions(id)
    const data = {
        book: book,
        suggestion: suggestion
    }
    return {
        props: {
            data
        }
    }
}
