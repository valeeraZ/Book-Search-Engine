export default function getAllBooks(page) {
    var requestOptions = {
        method: 'GET',
        redirect: 'follow'
    };

    return fetch(`${process.env.API_URI}/books?page=${page-1}`, requestOptions)
        .then(response => {return response.json()})
        .catch(error => console.log('error', error));
}
