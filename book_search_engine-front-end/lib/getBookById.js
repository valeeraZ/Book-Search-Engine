export default function getBookById(id) {
    var requestOptions = {
        method: 'GET',
        redirect: 'follow'
    };

    return fetch(`${process.env.API_URI}/books/${id}`, requestOptions)
        .then(response => {return response.json()})
        .catch(error => console.log('error', error));
}
