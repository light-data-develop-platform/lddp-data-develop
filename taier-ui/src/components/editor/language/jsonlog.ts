import * as monaco from 'monaco-editor';

const jsonlog = 'jsonlog';

// register json log for view json data with log informations
monaco.languages.register({ id: jsonlog });
monaco.languages.setMonarchTokensProvider(jsonlog, {
	tokenizer: {
		root: [
			// split
			[/^====================.*/gm, { token: 'comment' }],
			[/[;,.]/, 'delimiter'],

			// string
			[/"/, { token: 'string.quote', bracket: '@open', next: '@string' }],

			// numbers
			[/\d*\.\d+([eE][-+]?\d+)?/, 'number.float'],
			[/0[xX][0-9a-fA-F]+/, 'number.hex'],
			[/\d+.*/, 'number'],

			// boolean
			[/false|true/, 'keyword'],

			// chinese character
			[/[\u4e00-\u9fa5]+/, 'keyword'],
		],
		string: [
			[/[^\\"]+/, 'string'],
			[/\\./, 'string.escape.invalid'],
			[/"/, { token: 'string.quote', bracket: '@close', next: '@pop' }],
		],
	},
});
