import Layout from "../components/layout";
import {Stack, Switch, Typography} from "@mui/material";
import * as React from "react";
import getBooksByContent from "../lib/getBooksByContent";
import List from '@mui/material/List';
import ListItemText from '@mui/material/ListItemText';
import ListItemButton from '@mui/material/ListItemButton';
import getBooksByAuthor from "../lib/getBooksByAuthor";
import getBooksByTitle from "../lib/getBooksByTitle";
import Link from 'next/link'
import PropTypes from 'prop-types';
import Tabs from '@mui/material/Tabs';
import Tab from '@mui/material/Tab';
import Box from '@mui/material/Box';
import getRanking from "../lib/getRanking";

const style = {
    width: '100%',
    bgcolor: 'background.paper',
};

function TabPanel(props) {
    const {children, value, index, ...other} = props;

    return (
        <div
            role="tabpanel"
            hidden={value !== index}
            id={`simple-tabpanel-${index}`}
            aria-labelledby={`simple-tab-${index}`}
            {...other}
        >
            {value === index && (
                <Box sx={{p: 3}}>
                    <Typography>{children}</Typography>
                </Box>
            )}
        </div>
    );
}

TabPanel.propTypes = {
    children: PropTypes.node,
    index: PropTypes.number.isRequired,
    value: PropTypes.number.isRequired,
};

function a11yProps(index) {
    return {
        id: `simple-tab-${index}`,
        'aria-controls': `simple-tabpanel-${index}`,
    };
}

function listBooks(books) {
    return (
        <List sx={style} component="nav" aria-label="books searching result">
            {books.map((book) => (
                <Link href={`/book?id=${book.id}`} passHref key={book.id}>
                    <ListItemButton>
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
    const [value, setValue] = React.useState(0);
    const [checked, setChecked] = React.useState(false);

    const handleChange = (event, newValue) => {
        setValue(newValue);
    };
    const handleCheck = (event) => {
        setChecked(event.target.checked);
    };
    const keyword = data.keyword
    const books = data.data
    return (
        <Layout>
            <Tabs value={value} onChange={handleChange} aria-label="search results tabs" variant="scrollable"
                  scrollButtons="auto">
                <Tab label="search results by title" {...a11yProps(0)} />
                <Tab label="search results by content" {...a11yProps(1)} />
                <Tab label="search results by authors' names" {...a11yProps(2)} />
            </Tabs>
            <TabPanel value={value} index={0}>
                {books.booksByTitle.length > 0 ?
                    <div>
                        <Typography variant="h6" gutterBottom component="div" style={{marginTop: "2em"}}>
                            {books.booksByTitle.length} Results of books&apos; titles containing <i>{keyword}</i>
                        </Typography>
                        <Stack direction="row" spacing={1} alignItems="center">
                            <Typography>Ordered by </Typography>
                            <Typography variant="body2">relevance of keyword</Typography>
                            <Switch checked={checked} onChange={handleCheck}/>
                            <Typography variant="body2">Ordered by popularity(closeness centrality)</Typography>
                        </Stack>
                        {checked ? listBooks(books.booksByTitleOrdered) : listBooks(books.booksByTitle)}
                    </div>
                    :
                    <Typography variant="h6" gutterBottom component="div" style={{marginTop: "2em"}}>
                        No results found of books&apos; titles containing <i>{keyword}</i>
                    </Typography>
                }
            </TabPanel>
            <TabPanel value={value} index={1}>
                {books.booksByContent.length > 0 ?
                    <div>
                        <Typography variant="h6" gutterBottom component="div" style={{marginTop: "2em"}}>
                            {books.booksByContent.length} Results of books&apos; content containing <i>{keyword}</i>
                        </Typography>
                        <Stack direction="row" spacing={1} alignItems="center">
                            <Typography>Ordered by </Typography>
                            <Typography variant="body2">relevance of keyword</Typography>
                            <Switch checked={checked} onChange={handleCheck}/>
                            <Typography variant="body2">Ordered by popularity(closeness centrality)</Typography>
                        </Stack>
                        {checked ? listBooks(books.booksByContentOrdered) : listBooks(books.booksByContent)}
                    </div>
                    :
                    <Typography variant="h6" gutterBottom component="div" style={{marginTop: "2em"}}>
                        No results found of books&apos; content containing <i>{keyword}</i>
                    </Typography>
                }
            </TabPanel>
            <TabPanel value={value} index={2}>
                {books.booksByAuthor.length > 0 ?
                    <div>
                        <Typography variant="h6" gutterBottom component="div" style={{marginTop: "2em"}}>
                            {books.booksByAuthor.length} Results of books&apos; authors&apos; names containing <i>{keyword}</i>
                        </Typography>
                        <Stack direction="row" spacing={1} alignItems="center">
                            <Typography>Ordered by </Typography>
                            <Typography variant="body2">relevance of keyword</Typography>
                            <Switch checked={checked} onChange={handleCheck}/>
                            <Typography variant="body2">Ordered by popularity(closeness centrality)</Typography>
                        </Stack>
                        {checked ? listBooks(books.booksByAuthorOrdered) : listBooks(books.booksByAuthor)}
                    </div>
                    :
                    <Typography variant="h6" gutterBottom component="div" style={{marginTop: "2em"}}>
                        No results found of books&apos; authors&apos; names containing <i>{keyword}</i>
                    </Typography>
                }
            </TabPanel>
        </Layout>
    )

}

Array.prototype.unique = function () {
    var a = this.concat();
    for (var i = 0; i < a.length; ++i) {
        for (var j = i + 1; j < a.length; ++j) {
            if (a[i].id === a[j].id)
                a.splice(j--, 1);
        }
    }

    return a;
};

function sortBooksByRanking(books, ranking){
    books.sort((a, b) => {
      return ranking.indexOf(a.id) - ranking.indexOf(b.id)
    })
    return books
}

export async function getServerSideProps({query}) {
    const keywordOrRegex = query.keyword || "";

    const booksByContent = await getBooksByContent(keywordOrRegex, false)
    const booksByAuthor = await getBooksByAuthor(keywordOrRegex, false)
    const booksByTitle = await getBooksByTitle(keywordOrRegex, false)
    const ranking = await getRanking()
    const data = {
        keyword: keywordOrRegex,
        data: {
            booksByContent: booksByContent.unique(),
            booksByTitle: booksByTitle.unique(),
            booksByAuthor: booksByAuthor.unique(),
            booksByContentOrdered: booksByContent.unique().sort((a, b) => {return ranking.indexOf(a.id) - ranking.indexOf(b.id)}),
            booksByTitleOrdered: booksByTitle.unique().sort((a, b) => {return ranking.indexOf(a.id) - ranking.indexOf(b.id)}),
            booksByAuthorOrdered: booksByAuthor.unique().sort((a, b) => {return ranking.indexOf(a.id) - ranking.indexOf(b.id)})
        }
    }
    return {
        props: {
            data
        }
    }
}
