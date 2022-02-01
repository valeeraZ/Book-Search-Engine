import * as React from 'react';
import {useEffect, useState} from 'react';
import {alpha, styled} from '@mui/material/styles';
import AppBar from '@mui/material/AppBar';
import Box from '@mui/material/Box';
import InputBase from '@mui/material/InputBase';
import Toolbar from '@mui/material/Toolbar';
import SearchIcon from '@mui/icons-material/Search';
import Container from '@mui/material/Container';
import {useRouter} from 'next/router'
import Link from 'next/link'
import {MenuItem, TextField, Typography} from "@mui/material";
import Autocomplete from '@mui/material/Autocomplete';
import Head from 'next/head'

const Search = styled('div')(({ theme }) => ({
    position: 'relative',
    borderRadius: theme.shape.borderRadius,
    backgroundColor: alpha(theme.palette.common.white, 0.15),
    '&:hover': {
        backgroundColor: alpha(theme.palette.common.white, 0.25),
    },
    marginLeft: 0,
    width: '100%',
    [theme.breakpoints.up('sm')]: {
        marginLeft: theme.spacing(1),
        width: 'auto',
    },
}));

const SearchIconWrapper = styled('div')(({ theme }) => ({
    padding: theme.spacing(0, 2),
    height: '100%',
    position: 'absolute',
    pointerEvents: 'none',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
}));

const StyledAutocomplete = styled(Autocomplete)(({ theme }) => ({
    color: 'inherit',
    '& .MuiInputBase-input': {
        padding: theme.spacing(1, 1, 1, 0),
        // vertical padding + font size from searchIcon
        paddingLeft: `calc(1em + ${theme.spacing(4)})`,
        transition: theme.transitions.create('width'),
        width: '40ch',
        [theme.breakpoints.up('sm')]: {
            width: '40ch',
        },
    },
}));

//const fetcher = url => fetch(url).then(response => response.json());


export default function Layout({ children }) {
    const router = useRouter()
    return (
        <div>
            <Head>
                <title>Book Search Engine</title>
                <meta name="viewport" content="initial-scale=1.0, width=device-width" />
            </Head>
            <Box sx={{ flexGrow: 1 }}>
                <AppBar position="static">
                    <Toolbar>
                        <Link href="/" passHref>
                            <MenuItem>
                                <Typography
                                    variant="h6"
                                    noWrap
                                    component="div"
                                    sx={{ display: { xs: 'none', sm: 'block' } }}
                                >
                                    Book Search Engine
                                </Typography>
                            </MenuItem>
                        </Link>
                        <Search>
                            <SearchIconWrapper>
                                <SearchIcon />
                            </SearchIconWrapper>
                            <StyledAutocomplete
                                freeSolo
                                placeholder="Search books with keywords or RegEx"
                                id="free-solo-2-demo"
                                disableClearable
                                options={top100books.map((option) => option.title)}
                                renderInput={(params) => {
                                    const { InputLabelProps, InputProps, ...rest } = params;
                                    return <InputBase
                                        {...params.InputProps}
                                        {...rest}
                                        placeholder="Search by keyword or RegEx"
                                        style={{width: '40ch'}}
                                        onKeyPress={
                                            (event) => {
                                                if (event.key === "Enter") {
                                                    var keyword = event.target.value
                                                    if (keyword.length !== 0) {
                                                        router.push(`/search?keyword=${keyword}`)
                                                    } else {
                                                        router.push('/')
                                                    }
                                                }
                                            }
                                        }
                                    />;
                                }}
                            />
                        </Search>
                    </Toolbar>
                </AppBar>
            </Box>

            <main>
                <Container fixed>
                    {children}
                </Container>
            </main>
        </div>

    );
}

const top100books = [
    {title: "The Radio Gunner"},
    {title: "The Autobiography of an Ex-Colored Man"},
    {title: "In the Rockies with Kit Carson"},
    {title: "Nick Carter Stories No. 137, April 24, 1915"},
    {title: "The Tragedy of Pudd'nhead Wilson"},
    {title: "Deathworld"},
    {title: "The Stolen Brain; Or, A Wonderful Crime"},
    {title: "The Kreutzer Sonata and Other Stories"},
    {title: "The Secret of Toni"},
    {title: "The Sign of the Four"},
    {title: "A Christmas Carol: The original manuscript"},
    {title: "The Warden"},
    {title: "The Skylark of Space"},
    {title: "The Valley of Fear"},
    {title: "Black Diamonds: A Novel"},
    {title: "L'Assommoir"},
    {title: "Looking Backward, 2000 to 1887"},
    {title: "A Christmas Carol"},
    {title: "The Boy Scouts' Victory"},
    {title: "Erewhon; Or, Over the Range"},
    {title: "The Mysterious Stranger, and Other Stories"},
    {title: "Personal Recollections of Joan of Arc — Volume 1"},
    {title: "The Virginian: A Horseman of the Plains"},
    {title: "Resurrection"},
    {title: "Some Christmas Stories"},
    {title: "Narrative of the Life of Frederick Douglass, an American Slave"},
    {title: "The Return of the Native"},
    {title: "The Lock and Key Library: Classic Mystery and Detective Stories: Modern English"},
    {title: "The Deerslayer"},
    {title: "The Plot That Failed; or, When Men Conspire"},
    {title: "The House of the Dead; or, Prison Life in Siberia: with an introduction by Julius Bramont"},
    {title: "A Hero of Our Time"},
    {title: "Ghost Stories of an Antiquary"},
    {title: "Greensea Island: A Mystery of the Essex Coast"},
    {title: "Poirot Investigates"},
    {title: "The Voyages of Doctor Dolittle"},
    {title: "The Strange Case of Dr. Jekyll and Mr. Hyde"},
    {title: "Tanglewood Tales"},
    {title: "The Dorrington Deed-Box"},
    {title: "Lord Arthur Savile's Crime; The Portrait of Mr. W.H., and Other Stories"},
    {title: "Best Russian Short Stories"},
    {title: "Kwaidan: Stories and Studies of Strange Things"},
    {title: "Barchester Towers"},
    {title: "A Rogue by Compulsion: An Affair of the Secret Service"},
    {title: "Something New"},
    {title: "A Christmas Carol in Prose; Being a Ghost Story of Christmas"},
    {title: "Persuasion"},
    {title: "Ozma of Oz: A Record of Her Adventures with Dorothy Gale of Kansas, the Yellow Hen, the Scarecrow, the Tin Woodman, Tiktok, the Cowardly Lion, and the Hungry Tiger; Besides Other Good People too Numerous to Mention Faithfully Recorded Herein"},
    {title: "The Arabian Nights Entertainments"},
    {title: "The Wind in the Willows"},
    {title: "Dorothy and the Wizard in Oz"},
    {title: "The Hound of the Baskervilles"},
    {title: "Carmen"},
    {title: "In Caverns Below"},
    {title: "Arsène Lupin versus Herlock Sholmes"},
    {title: "The Extraordinary Adventures of Arsène Lupin, Gentleman-Burglar"},
    {title: "Tales and Fantasies"},
    {title: "Ethan Frome"},
    {title: "The Secret Agent: A Simple Tale"},
    {title: "The Bostonians, Vol. I (of II)"},
    {title: "His Last Bow: An Epilogue of Sherlock Holmes"},
    {title: "The Gilded Age: A Tale of Today"},
    {title: "Three Ghost Stories"},
    {title: "The Mantle, and Other Stories"},
    {title: "The Hungry Stones, and Other Stories"},
    {title: "The Coral Island: A Tale of the Pacific Ocean"},
    {title: "The Awakening of Spring: A Tragedy of Childhood"},
    {title: "Carmilla"},
    {title: "The Hollow Needle; Further adventures of Arsène Lupin"},
    {title: "American Fairy Tales"},
    {title: "Treasure Island"},
    {title: "The Battle of the Books, and other Short Pieces"},
    {title: "The Aspern Papers"},
    {title: "Sunshine Sketches of a Little Town"},
    {title: "Ghost Stories of an Antiquary Part 2: More Ghost Stories"},
    {title: "Cranford"},
    {title: "Oblomov"},
    {title: "The Thirty-Nine Steps"},
    {title: "The Life and Adventures of Santa Claus"},
    {title: "A Tale of Two Cities"},
    {title: "Another Man's Shoes"},
    {title: "The Mayor of Casterbridge"},
    {title: "Northanger Abbey"},
    {title: "Weird Tales. Vol. 1 (of 2)"},
    {title: "Greenmantle"},
    {title: "Under the Greenwood Tree; Or, The Mellstock Quire: A Rural Painting of the Dutch School"},
    {title: "Silver Rags"},
    {title: "Ralph on the Midnight Flyer: or, The Wreck at Shadow Valley"},
    {title: "Joseph Andrews, Vol. 1"},
    {title: "The Garnet Story Book: Tales of Cheer Both Old and New"},
    {title: "Tales of Terror and Mystery"},
    {title: "A Voyage to Arcturus"},
    {title: "Heart of Darkness"},
    {title: "Complete Original Short Stories of Guy De Maupassant"},
    {title: "Fathers and Sons"},
    {title: "The House of Mirth"},
    {title: "Bleak House"},

]
