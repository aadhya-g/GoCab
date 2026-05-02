const axios = require("axios");

async function getDistanceKm(from, to) {
    const url = `https://router.project-osrm.org/route/v1/driving/${from.lon},${from.lat};${to.lon},${to.lat}?overview=false`;

    const res = await axios.get(url);
    return res.data.routes[0].distance / 1000;
}

module.exports = getDistanceKm;

