import { configuration, validate, run } from '../common/scenarios.js';
export const options=configuration(true);
export function setup(){validate('mensajeria');}
export default function(){run('mensajeria');}
