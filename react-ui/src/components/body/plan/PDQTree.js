import React from 'react';
import { Group } from '@vx/group';
import { Tree } from '@vx/hierarchy';
import { LinearGradient } from '@vx/gradient';
import { hierarchy } from 'd3-hierarchy';
import { pointRadial } from 'd3-shape';
import { LinkHorizontal,
         LinkVertical,
         LinkRadial
} from '@vx/shape';

/**
 * PDQTree creates an svg of the plan search tree.
 * Visualized in GraphicalPlanModal.
 *
 */

export default class PDQTree extends React.Component {
  constructor(props){
    super(props);
    this.onDragStart = this.onDragStart.bind(this);
    this.onDragMove = this.onDragMove.bind(this);
    this.onDragEnd = this.onDragEnd.bind(this);
    this.onWheel = this.onWheel.bind(this);
    this.pan = this.pan.bind(this);
    this.zoom = this.zoom.bind(this);
  }
  state = {
    layout: 'polar',
    orientation: 'horizontal',
    stepPercent: 0.5,
    selectedNode: null,
    matrix: [1, 0, 0, 1, 30, 30],
    dragging: false,
    startX: 0,
    startY: 0
  };

  //Translational methods
  onDragStart(e) {
    // Find start position of drag based on touch/mouse coordinates.
    const startX = typeof e.clientX === 'undefined' ? e.changedTouches[0].clientX : e.clientX;
    const startY = typeof e.clientY === 'undefined' ? e.changedTouches[0].clientY : e.clientY;

    const state = {
      dragging: true,
      startX,
      startY,
    };

    this.setState(state);
  }

  onDragMove(e) {
    // If not dragging, just return
    if (!this.state.dragging) {
      return;
    }

    // Get the new x coordinates
    const x = typeof e.clientX === 'undefined' ? e.changedTouches[0].clientX : e.clientX;
    const y = typeof e.clientY === 'undefined' ? e.changedTouches[0].clientY : e.clientY;

    const dx = x - this.state.startX;
    const dy = y - this.state.startY;

    // Pan using the deltas
    this.pan(dx, dy);

    this.setState({
      startX: x,
      startY: y,
    });
  }

  onDragEnd() {
    this.setState({ dragging: false });
  }

  onWheel(e) {
    if (e.deltaY < 0) {
      this.zoom(1.05);
    } else {
      this.zoom(0.95);
    }
  }

  pan(dx, dy) {
    const m = this.state.matrix;
    m[4] += dx;
    m[5] += dy;
    this.setState({ matrix: m });
  }

  zoom(scale) {
    const m = this.state.matrix;
    const len = m.length;
    for (let i = 0; i < len; i++) {
      m[i] *= scale;
    }
    m[4] += (1 - scale) * this.props.width / 2;
    m[5] += (1 - scale) * this.props.height / 2;
    this.setState({ matrix: m });
  }

  render() {
    const {
      width,
      height,
      margin = {
        top: 30,
        left: 30,
        right: 30,
        bottom: 30
      }
    } = this.props;

    const { layout, orientation, stepPercent } = this.state;

    const innerWidth = width - margin.left - margin.right;
    const innerHeight = height - margin.top - margin.bottom;

    let origin;
    let sizeWidth;
    let sizeHeight;

    let data = this.props.data;

    if (layout === 'polar') {
      origin = {
        x: innerWidth / 2,
        y: innerHeight / 2
      };
      sizeWidth = 2 * Math.PI;
      sizeHeight = Math.min(innerWidth, innerHeight) / 2;
    } else {
      origin = { x: 0, y: 0 };
      if (orientation === 'vertical') {
        sizeWidth = innerWidth;
        sizeHeight = innerHeight;
      } else {
        sizeWidth = innerHeight;
        sizeHeight = innerWidth;
      }
    }

    return (
      <div>
        <svg
          width={width}
          height={height}
          onMouseDown={this.onDragStart}
          onTouchStart={this.onDragStart}
          onMouseMove={this.onDragMove}
          onTouchMove={this.onDragMove}
          onMouseUp={this.onDragEnd}
          onTouchEnd={this.onDragEnd}
          onWheel={this.onWheel}>

          <LinearGradient id="lg" from="#E0E0E0" to="#fe6e9e" />

          <rect width={width} height={height} rx={14} fill="#F0F0F0" />

          <Group
            top={margin.top}
            left={margin.left}
            transform={`matrix(${this.state.matrix.join(' ')})`}>

            <Tree
              root={hierarchy(data, d => ( d.children))}
              size={[sizeWidth, sizeHeight]}
              separation={(a, b) => (a.parent === b.parent ? 1 : 0.5) / a.depth}>
              {data => (
                <Group top={origin.y} left={origin.x}>
                  {data.links().map((link, i) => {
                    let LinkComponent;
                    if (layout === 'polar') {
                      LinkComponent = LinkRadial;
                    }else {
                      if (orientation === 'vertical') {
                        LinkComponent = LinkVertical;
                    }else {
                        LinkComponent = LinkHorizontal;
                      }
                    }
                    return (
                      <LinkComponent
                        data={link}
                        percent={stepPercent}
                        stroke="#374469"
                        strokeWidth="1"
                        fill="none"
                        key={i}
                        onClick={data => event => {
                        }}
                      />
                    );
                  })}

                  {data.descendants().map((node, key) => {
                    const width = 25;
                    const height = 15;

                    let top;
                    let left;
                    if (layout === 'polar') {
                      const [radialX, radialY] = pointRadial(node.x, node.y);
                      top = radialY;
                      left = radialX;
                    } else {
                      if (orientation === 'vertical') {
                        top = node.y;
                        left = node.x;
                      } else {
                        top = node.x;
                        left = node.y;
                      }
                    }

                    return (
                      <Group top={top} left={left} key={key}>
                        {node.depth === 0 && (
                          <circle
                            r={12}
                            fill='#428bca'
                            onClick={() => {
                              this.setState({ selectedNode: node.data });
                              this.forceUpdate();
                            }}
                            id={node+key}
                          />
                        )}
                        {node.depth !== 0 && (
                          <rect
                            height={height}
                            width={width}
                            y={-height / 2}
                            x={-width / 2}
                            fill={node.data.children ? '#428bca' : node.data.type === "SUCCESSFUL" ? "#5cb85c" : '	#d9534f'}
                            rx={!node.data.children ? 10 : 0}
                            onClick={(e) => {
                              this.setState({ selectedNode: node.data });
                              this.forceUpdate();
                            }}
                            id={node+key}
                          />
                        )}
                        <text
                          dy={'.33em'}
                          fontSize={9}
                          fontFamily="Arial"
                          textAnchor={'middle'}
                          style={{ pointerEvents: 'none' }}
                          fill={node.depth === 0 ? 'white' : node.children ? 'white' : 'black'}
                        >
                          {node.data.id}
                        </text>
                      </Group>
                    );
                  })}
                </Group>
              )}
            </Tree>
          </Group>

        </svg>

        <div style={{ color: 'black', fontSize: 15, display:"flex" }}>
          <div style={{margin:"1rem 1rem 1rem 1rem"}}>
            <label>Layout:</label>{" "}
            <select
              onClick={e => e.stopPropagation()}
              onChange={e => this.setState({ layout: e.target.value })}
              value={layout}
            >
              <option value="cartesian">cartesian</option>
              <option value="polar">polar</option>
            </select>
          </div>

          <div style={{margin:"1rem 1rem 1rem 1rem"}}>
          <label>Orientation: </label>{" "}
          <select
            onClick={e => e.stopPropagation()}
            onChange={e => this.setState({ orientation: e.target.value })}
            value={orientation}
            disabled={layout === 'polar'}
          >
            <option value="vertical">vertical</option>
            <option value="horizontal">horizontal</option>
          </select>
          </div>

          <div style={{margin:"1rem 1rem 1rem 1rem"}}>
          <span>
            {this.state.selectedNode === null ?
              <div style={{width: "20rem", height: "3rem"}}>
              Click on a node for its information.
              </div>
              :
            <div style={{width: "20rem", height: "3rem", display:"flex",
                          flexDirection: "row"}}>
              <div style={{margin:"0 .5rem 0 .5rem", width: "3rem" }}>
              <b>Node: </b> <br/>{this.state.selectedNode.id}
              </div>

              {this.state.selectedNode.accessTerm === null ?
              null
              :
              <div style={{margin:"0 .5rem 0 .5rem",
                            overflow:"hidden",
                            whiteSpace:"nowrap",
                            textOverflow: "ellipsis"}}>
              <b>Access Term: </b> <br/> {this.state.selectedNode.accessTerm}
              </div>
              }
              <div style={{margin:"0 .5rem 0 .5rem", width: "6rem"}}>
              <b>Node Type: </b> <br/> {this.state.selectedNode.type}
              </div>
            </div>
          }
          </span>
          </div>
        </div>
      </div>
    );
  }
}
