export default function getBooksByAuthor(author) {
    var requestOptions = {
        method: 'GET',
        redirect: 'follow'
    };

    return fetch(`${process.env.API_URI}/books?searchByAuthor=${author}`, requestOptions)
        .then(response => {return response.json()})
        .catch(error => console.log('error', error));
}
