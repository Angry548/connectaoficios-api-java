import { configuration, validate, run } from '../common/scenarios.js';
export const options=configuration(false);
export function setup(){validate('solicitudes');}
export default function(){run('solicitudes');}
