import Layout from "../components/layout";
import getBookTextById from "../lib/getBookText";
import {Typography} from "@mui/material";
import {useRouter} from "next/router";
import ArrowBackIosIcon from "@mui/icons-material/ArrowBackIos";
import Button from "@mui/material/Button";
import * as React from "react";

export default function Text({book}){
    const router = useRouter()
    return(
        <Layout>
            <Button onClick={() => router.back()} style={{marginTop:"2em"}}>
                <ArrowBackIosIcon/> Back
            </Button>
            <Typography style={{whiteSpace: "pre-wrap"}}>
                {book}
            </Typography>
        </Layout>
    )
}

export async function getServerSideProps({query}) {
    const id = query.id || 1;
    const book = await getBookTextById(id)
    return {
        props: {
            book
        }
    }
}
