export default function getBooksBySuggestions(id) {
    var requestOptions = {
        method: 'GET',
        redirect: 'follow'
    };

    return fetch(`${process.env.API_URI}/books?suggestion=${id}`, requestOptions)
        .then(response => {return response.json()})
        .catch(error => console.log('error', error));
}
