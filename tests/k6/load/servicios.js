import { configuration, validate, run } from '../common/scenarios.js';
export const options=configuration(false);
export function setup(){validate('servicios');}
export default function(){run('servicios');}
