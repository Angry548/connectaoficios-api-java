import { configuration, validate, run } from '../common/scenarios.js';
export const options=configuration(true);
export function setup(){validate('solicitudes');}
export default function(){run('solicitudes');}
