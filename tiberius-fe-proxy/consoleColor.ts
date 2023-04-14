
type Color = 'green' | 'cyan';

const reset = '\x1b[0m'
const dim = '\x1b[2m'
const cyan = '\x1b[36m'
const green = '\x1b[32m'
const red = '\x1b[31m'

const setColor = (value, color) => {
  return `${color}${value}${reset}`
}

export const cc = {
  green: (value: string) => {
    return setColor(value, green);
  },
  cyan: (value: string) => {
    return setColor(value, cyan);
  },
  dim: (value: string) => {
    return setColor(value, dim);
  },
  red: (value: string) => {
    return setColor(value, red);
  }
}