const axios = require("axios");

async function geocode(place) {
    const response = await axios.get(
        "https://nominatim.openstreetmap.org/search",
        {
            params: {
                q: `${place}, India`,
                format: "json",
                limit: 1,
                countrycodes: "in"
            },
            headers: { "User-Agent": "GoCab-App" }
        }
    );

    if (!response.data.length) return null;

    return {
        lat: parseFloat(response.data[0].lat),
        lon: parseFloat(response.data[0].lon)
    };
}

module.exports = geocode;
