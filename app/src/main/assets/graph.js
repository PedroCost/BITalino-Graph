window.onload = function () {
    generateGraph();
}

var margin = { top: 20, right: 20, bottom: 30, left: 40 }
    , width = window.innerWidth - margin.left - margin.right
    , height = window.innerHeight - margin.top - margin.bottom
    , counter = 0
    , line
    , plotLine
    , data = [];

function generateGraph(){

        var xScale = d3.scaleLinear()
            .range([0, width])
            .domain([0, 30]);

        var yScale = d3.scaleLinear()
            .range([height, 0])
            .domain([0, 1024]).nice();

        var yAxis = d3.axisLeft(yScale).ticks(8),
            xAxis = d3.axisBottom(xScale).ticks(6);

        var svg = d3.select("#plot").append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom);

        var yAxisDraw = svg.append("g")
            .attr("class", "y axis")
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")")
            .attr('id', "axis--y")
            .call(yAxis);

        var xAxisDraw = svg.append("g")
            .attr("class", "x axis ")
            .attr('id', "axis--x")
            .attr("transform", "translate(" + margin.left + "," + (height + margin.top) + ")")
            .call(xAxis);

        plotLine = d3.line()
            .curve(d3.curveLinear)
            .x(function (d, i) {
                if(counter > 1)
                    return xScale(i + counter - 1);
                return xScale(i + counter);
            })
            .y(function (d) {
                return yScale(d);
            });

        line = svg.append("g").append("path").attr("transform", "translate(" + margin.left + "," + margin.top + ")")
            .datum(data)
            .attr("d", plotLine)
            .attr("id", "linha");
}

function update(value) {
    if (data.length > 30) {
        data.shift(data.push(value));
    } else {
        data.push(value);
    }

    line.datum(data)
        .attr("d", plotLine)
        .style("fill", "none")
        .style("stroke", "brown");
}

function valueFromBitalino(value){
    update(value + 0);
}