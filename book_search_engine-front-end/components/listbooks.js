import * as React from 'react';
import { styled } from '@mui/material/styles';
import Grid from '@mui/material/Grid';
import {IconButton, Typography} from "@mui/material";
import Paper from '@mui/material/Paper';
import Link from 'next/link'

const Book = styled(Paper)(({ theme }) => ({
    display: 'flex',
    flexDirection:'column',
    padding: theme.spacing(3),
}));

export default function ListBooks({ books }) {
    return (
        <Grid container columns={{ xs: 4, sm: 8, md: 12 }} spacing={2} style={{marginBottom:"2em"}}>
            {books.map((book) => (
                <Grid item xs={4} sm={4} md={3} key={book.id}>
                    <Link href={`/book?id=${book.id}`} passHref>
                        <IconButton>
                        <Book>
                            <img
                                src={`${ (book.image).replace('small', 'medium')}`}
                                srcSet={`${ (book.image).replace('small', 'medium')}`}
                                alt={book.title}
                                loading="lazy"
                                height="100%"
                            />
                            <Typography variant="h6" gutterBottom component="div">
                                {book.title.slice(0, 100)}
                                {book.title.length > 100 ? "..." : ""}
                            </Typography>
                            {book.authors.map((author) => (
                                <Typography variant="subtitle2" key={author.name}>
                                    {(author.name).replace(', ', '-')}
                                </Typography>
                            ))}
                        </Book>
                        </IconButton>
                    </Link>
                </Grid>
            ))}
        </Grid>
    )
}
