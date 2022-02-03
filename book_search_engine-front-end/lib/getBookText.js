export default function getBookTextById(id) {
    var requestOptions = {
        method: 'GET',
        redirect: 'follow'
    };

    return fetch(`${process.env.API_URI}/text/${id}`, requestOptions)
        .then(response => {return response.text()})
        .catch(error => console.log('error', error));
}
