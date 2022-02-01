export default function getBooksByTitle(title) {
    var requestOptions = {
        method: 'GET',
        redirect: 'follow'
    };

    return fetch(`${process.env.API_URI}/books?searchByTitle=${title}`, requestOptions)
        .then(response => {return response.json()})
        .catch(error => console.log('error', error));
}
