import getBookById from "../lib/getBookById";
import {Stack, Typography} from "@mui/material";
import * as React from "react";
import BookIcon from '@mui/icons-material/Book';
import Link from "@mui/material/Link";
import Layout from "../components/layout";
import {useRouter} from "next/router";
import Button from '@mui/material/Button';
import ArrowBackIosIcon from '@mui/icons-material/ArrowBackIos';

export default function Book({book}) {
    const router = useRouter()
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
                    <BookIcon/> <Link href={book.text} variant="subtitle1">Read this book online</Link>
                </div>
            </Stack>
        </Layout>
    )

}

export async function getServerSideProps({query}) {
    const id = query.id || 1;
    const book = await getBookById(id)
    return {
        props: {
            book
        }
    }
}
