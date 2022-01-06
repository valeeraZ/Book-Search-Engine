import Layout from "../components/layout";
import ListBooks from "../components/listbooks";
import getAllBooks from "../lib/getAllBooks";
import {useRouter} from 'next/router'
import Pagination from '@mui/material/Pagination';
import * as React from "react";
import {Box, Stack, Typography} from "@mui/material";

export default function Home({data}) {
    const router = useRouter()
    // When new page selected in pagination, we take current path and query params.
    // Then add or modify page param and then navigate to the new route.
    const paginationHandler = (event, page) => {
        const currentPath = router.pathname;
        const currentQuery = router.query;
        currentQuery.page = page;

        router.push({
            pathname: currentPath,
            query: currentQuery,
        });
    };

    return (
        <Layout>
            <Typography variant="h5" gutterBottom component="div" style={{marginTop: "2em"}}>
                Book Search Engine - A Web Application for indexing books by keywords or ReGex
            </Typography>
            <ListBooks books={data.result}/>
            <Box justifyContent="center" display="flex" alignItems="center">
                <Pagination style={{marginBottom:"2em", textAlign: "center"}} size="large" count={data.pageCount} color="primary" page={data.currentPage + 1} onChange={paginationHandler} />
            </Box>
        </Layout>
    )
}

export async function getServerSideProps({query}) {
    const page = query.page || 1;
    const data = await getAllBooks(page)
    return {
        props: {
            data
        }
    }
}
